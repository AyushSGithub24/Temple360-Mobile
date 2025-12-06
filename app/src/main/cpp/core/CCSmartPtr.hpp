#ifndef VR720_CCSMARTPTR_HPP
#define VR720_CCSMARTPTR_HPP

#include <unordered_map>

// When a template class is a friend, it must be declared first
template <typename T>
class CCSmartPtr;

//Auxiliary class
class CCUPtr
{
public:

    // The constructor's parameter is a pointer to the base object
    CCUPtr(void* ptr);

    //Destructor
    ~CCUPtr();

    //Reference count
    int count;

    //Base object pointer
    void *p;
};

//Pointer pool
class CCPtrPool {

public:

    std::unordered_map<void*, CCUPtr*> pool;

    static bool IsStaticPoolCanUse();
    static CCPtrPool* GetStaticPool();
    static void InitPool();
    static void ReleasePool();

    CCUPtr* GetPtr(void* ptr);
    CCUPtr* AddPtr(void* ptr);
    CCUPtr* AddRefPtr(void* ptr);
    CCUPtr* RemoveRefPtr(void* ptr);
    void ReleasePtr(void* ptr);
    void ClearUnUsedPtr();

    void ReleaseAllPtr();
};
#define CCPtrPoolStatic CCPtrPool::GetStaticPool()
#define CCPtrPoolStaticCanUse CCPtrPool::IsStaticPoolCanUse()
//Smart pointer class
template <typename T>
class CCSmartPtr
{
private:
    T* ptr = nullptr;
    CCUPtr* rp = nullptr;  //Auxiliary class object pointer
public:
    CCSmartPtr() {  
        ptr = nullptr;
        if(CCPtrPoolStaticCanUse)
            rp = CCPtrPoolStatic->AddPtr(nullptr);
    }
    //Constructor
    CCSmartPtr(T *srcPtr)  { 
        ptr = srcPtr;
        if(CCPtrPoolStaticCanUse)
            rp = CCPtrPoolStatic->AddPtr(srcPtr);
    }      
    //Copy constructor
    CCSmartPtr(const CCSmartPtr<T> &sp)  {      
        ptr = sp.ptr;
        if(CCPtrPoolStaticCanUse)
            rp = CCPtrPoolStatic->AddRefPtr(sp.ptr);
    }     

    //Overload assignment operator
    CCSmartPtr& operator = (const CCSmartPtr<T>& rhs) {
        if(CCPtrPoolStaticCanUse)
            CCPtrPoolStatic->RemoveRefPtr(ptr);
        ptr = rhs.ptr;
        if(CCPtrPoolStaticCanUse)
            rp = CCPtrPoolStatic->AddRefPtr(ptr);
        return *this;
    }

    T & operator *() const { //Overload * operator
        return *ptr;
    }
    T* operator ->() const { //Overload -> operator
        return ptr;
    }

    bool IsNullptr() const {
        return ptr == nullptr;
    }
    T* GetPtr() const { return ptr;  }
    int CheckRef() {
        if (rp) return rp->count;
        return 0;
    }
    int AddRef() {
        if (rp) 
            return ++rp->count;
        return 0;
    }
    void ForceRelease() {
        if(CCPtrPoolStaticCanUse)
            CCPtrPool::GetStaticPool()->ReleasePtr(ptr);
        ptr = nullptr;
        *rp = nullptr;
    }

    ~CCSmartPtr() {        //Destructor
        if(CCPtrPoolStaticCanUse)
            CCPtrPoolStatic->RemoveRefPtr(ptr);
        ptr = nullptr;
        rp = nullptr;
    }

};


#endif //VR720_CCSMARTPTR_HPP
